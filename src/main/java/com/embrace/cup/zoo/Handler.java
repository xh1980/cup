package com.embrace.cup.zoo;

import java.util.List;

public interface  Handler {

    ResponseWeb handle (Context ctx);
    List<String> allowedMethods();
    Boolean LoginRequired();
    Boolean PermissionRequired();
}
