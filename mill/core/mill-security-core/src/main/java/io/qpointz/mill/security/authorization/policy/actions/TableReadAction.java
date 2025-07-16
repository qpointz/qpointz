package io.qpointz.mill.security.authorization.policy.actions;

import io.qpointz.mill.security.authorization.policy.Action;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class TableReadAction implements Action {

    @Getter
    private final List<String> tableName;

    @Override
    public List<String> subject() {
        return this.tableName;
    }

    @Override
    public String actionName() {
        return "rel-read";
    }
}
