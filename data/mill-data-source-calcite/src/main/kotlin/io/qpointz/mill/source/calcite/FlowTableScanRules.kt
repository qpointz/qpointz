package io.qpointz.mill.source.calcite

import org.apache.calcite.adapter.enumerable.EnumerableConvention
import org.apache.calcite.adapter.enumerable.EnumerableTableScan
import org.apache.calcite.plan.Convention
import org.apache.calcite.rel.RelNode
import org.apache.calcite.rel.convert.ConverterRule

class FlowTableScanToEnumerableRule private constructor(
    config:Config
): ConverterRule(config) {

    override fun convert(rel: RelNode?): RelNode? {
        val scan = rel as FlowTableScan
        val table = scan.table
        return if (EnumerableTableScan.canHandle(table)) EnumerableTableScan.create(scan.cluster, table) else null
    }

    companion object {

        @JvmField
        val INSTANCE = FlowTableScanToEnumerableRule(Config.INSTANCE
            .withConversion(
                FlowTableScan::class.java,
                { scan -> EnumerableTableScan.canHandle(scan.table)},
                Convention.NONE,
                EnumerableConvention.INSTANCE,
                "FlowTableScanToEnumerableRule"
            )
            .withRuleFactory (::FlowTableScanToEnumerableRule)
         )

    }

}