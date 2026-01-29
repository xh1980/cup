package com.embrace.cup.zoo;

import java.util.Map;


public interface  Handler {

    ResponseWeb handle (Map<String, Object> params);
}
