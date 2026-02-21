package io.qpointz.mill.services.jdbc;


import lombok.*;

import java.util.Optional;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JdbcCalciteConfiguration {

    @Getter
    @Setter
    private String url;

    @Getter
    @Setter
    private String driver;

    @Getter
    @Setter
    @Builder.Default
    private Optional<String> user = Optional.empty();

    @Getter
    @Setter
    @Builder.Default
    private Optional<String> password = Optional.empty();

    @Getter
    @Setter
    @Builder.Default
    private Optional<String> targetSchema= Optional.empty();

    @Getter
    @Setter
    @Builder.Default
    private Optional<String> schema= Optional.empty();

    @Getter
    @Setter
    @Builder.Default
    private Optional<String> catalog= Optional.empty();

    @Getter
    @Setter
    @Builder.Default
    private Boolean multiSchema= false;

}
