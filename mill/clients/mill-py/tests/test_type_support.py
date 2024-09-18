import base64
import os
import unittest
import uuid
from dataclasses import dataclass

from whenever import LocalDateTime

from millclient import utils, MillError
from millclient.proto.io.qpointz.mill import VectorBlock, LogicalDataTypeLogicalDataTypeId


@dataclass(init=True)
class RefData:
    key : str
    idx : int
    type : LogicalDataTypeLogicalDataTypeId
    isnull : bool
    value : str
    hints : list[str]

    def parse(self):
        if self.isnull:
            return None
        match self.type:
            case LogicalDataTypeLogicalDataTypeId.STRING:
                return self.value
            case LogicalDataTypeLogicalDataTypeId.BOOL:
                return self.value.upper() == 'TRUE'
            case LogicalDataTypeLogicalDataTypeId.INT:
                return int(self.value)
            case LogicalDataTypeLogicalDataTypeId.SMALL_INT:
                return int(self.value)
            case LogicalDataTypeLogicalDataTypeId.TINY_INT:
                return int(self.value)
            case LogicalDataTypeLogicalDataTypeId.BIG_INT:
                return int(self.value)
            case LogicalDataTypeLogicalDataTypeId.DOUBLE:
                return float(self.value)
            case LogicalDataTypeLogicalDataTypeId.FLOAT:
                return float(self.value)
            case LogicalDataTypeLogicalDataTypeId.BINARY:
                return base64.b64decode(self.value)
            case LogicalDataTypeLogicalDataTypeId.UUID:
                return uuid.UUID(self.value)
            case LogicalDataTypeLogicalDataTypeId.DATE:
                return LocalDateTime.strptime(self.value, "%Y-%m-%d").date()
            case LogicalDataTypeLogicalDataTypeId.TIME:
                ldt = LocalDateTime.strptime(self.value[:-10], "%H:%M:%S")
                nanos = int(self.value[9:])
                return ldt.add(nanoseconds=nanos, ignore_dst=True).time()
            case LogicalDataTypeLogicalDataTypeId.TIMESTAMP_TZ:
                ldt = LocalDateTime.strptime(self.value[:-10], "%Y-%m-%d %H:%M:%S")
                nanos = int(self.value[-9:])
                return ldt.add(nanoseconds=nanos, ignore_dst=True).assume_tz("UTC", disambiguate="raise")
            case LogicalDataTypeLogicalDataTypeId.TIMESTAMP:
                ldt = LocalDateTime.strptime(self.value[:-10], "%Y-%m-%d %H:%M:%S")
                nanos = int(self.value[-9:])
                return ldt.add(nanoseconds=nanos, ignore_dst=True)
            case _:
                raise MillError(f"Not Parsable {self}")


def vector_block_suite(suite:str) -> tuple[VectorBlock, dict, dict]:
    vb:VectorBlock = VectorBlock()

    pfx = os.path.join(os.path.abspath(os.path.dirname(__file__)), suite)
    with open(f"{pfx}.bin", 'rb') as f:
        data = f.read()
        vb = vb.parse(data)

    fields_idx = {}
    for f in vb.schema.fields:
        fields_idx.update({f.name:f.field_idx})

    with open(f"{pfx}.ref", 'r') as f:
        #l = f.readlines()
        lines = [line[:-1].split("|") for line in f]
        reference = {}
        for l in lines:
            d = RefData(key = l[0],
                    idx = int(l[1]),
                    type = LogicalDataTypeLogicalDataTypeId.from_string(l[2]),
                    isnull = l[3] == 'TRUE',
                    value = l[4],
                    hints = l[5:])
            if (d.key not in reference):
                reference[d.key] = []
            reference[d.key].append(d)
    return vb,fields_idx,reference

class MillClientTests(unittest.TestCase):

    def do_test(self, vb:VectorBlock, key:str, idx: int, refs:list):
        v = vb.vectors[idx]
        type = refs[0].type
        vals = utils.vector_to_array(type, v)
        for idx in range(len(vals)):
            ref = refs[idx]
            val = vals[idx]
            if ref.isnull:
                self.assertIsNone(val, f"{key}:{type} #{idx} expected to be None")
            else:
                pv = ref.parse()
                if type == LogicalDataTypeLogicalDataTypeId.DOUBLE or type == LogicalDataTypeLogicalDataTypeId.FLOAT:
                    self.assertAlmostEqual(pv, val, msg=f"{key}:{type} #{idx} expected Ref~=Value:{pv}~={val}")
                else:
                    self.assertEqual(pv, val, f"{key}:{type} #{idx} expected Ref==Value {pv}=={val}")


    def test_converters(self):
        vb, idx, ref = vector_block_suite("../../../test/messages/logical-types")
        for k, v in idx.items():
            with self.subTest(f"Read {k}[{v}]"):
                self.do_test(vb, k, v, ref[k])
        with self.subTest("Read To Pandas"):
            df = utils.vector_block_to_pandas(vb)


if __name__ == '__main__':
    unittest.main()