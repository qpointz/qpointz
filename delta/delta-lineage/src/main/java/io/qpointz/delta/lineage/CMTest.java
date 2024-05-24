package io.qpointz.delta.lineage;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.jsoup.Jsoup;

import javax.swing.text.Element;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class CMTest {

    public static void main(String[] args) throws IOException {
        val p = Parser.builder().build();
        val f = new File("./test.md");
        var r = new FileReader(f);
        val node = p.parseReader(r);
        Visitor visitor = new AbstractVisitor() {

            @Override
            public void visit(HtmlInline htmlInline) {
                super.visit(htmlInline);
            }

            @Override
            public void visit(HtmlBlock htmlBlock) {
                val d = Jsoup.parse(htmlBlock.getLiteral(), "", org.jsoup.parser.Parser.xmlParser());
                super.visit(htmlBlock);
            }
        };

        node.accept(visitor);

    }
}
