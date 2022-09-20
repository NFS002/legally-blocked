package blokd.client

import jsonrpc4kt.converters.jackson
import jsonrpc4kt.core.authentication
import jsonrpc4kt.core.basicAuth
import jsonrpc4kt.core.conversion
import jsonrpc4kt.core.jsonRpcClient

class Client {

    val client = jsonRpcClient("localhost", 3051) {
        authentication {
            basicAuth("testuser", "pass123")
        }

        conversion {
            jackson {
                configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
