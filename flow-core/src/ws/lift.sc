val pf: PartialFunction[Int, Int] = {case i:Int if i>0 => i * 2}

pf.lift(-2)