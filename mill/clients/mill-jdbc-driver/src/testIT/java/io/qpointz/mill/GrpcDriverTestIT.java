package io.qpointz.mill;

import lombok.extern.java.Log;
import lombok.val;

@Log
public class GrpcDriverTestIT extends BaseDriverTestIT {

    @Override
    String getConnectionUrl() {
        return String.format("jdbc:mill://%s:%s", getMillHost(), getMillPort());
    }

}
