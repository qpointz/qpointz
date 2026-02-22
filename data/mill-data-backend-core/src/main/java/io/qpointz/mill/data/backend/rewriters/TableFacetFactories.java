package io.qpointz.mill.data.backend.rewriters;

public class TableFacetFactories {

    private TableFacetFactories() {
        //empty constructor to hide public
    }

    private record StaticCollectionFacetFactory(TableFacetsCollection facets) implements TableFacetFactory {
    }

    public static TableFacetFactory fromCollection(TableFacetsCollection facets) {
        return new StaticCollectionFacetFactory(facets);
    }

}
