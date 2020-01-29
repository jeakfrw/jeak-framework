package de.fearnixx.jeak.service.controller.testImpls;

import de.fearnixx.jeak.reflect.RequestBody;
import de.fearnixx.jeak.reflect.RequestMapping;
import de.fearnixx.jeak.reflect.RequestParam;
import de.fearnixx.jeak.reflect.RestController;
import de.fearnixx.jeak.service.controller.IResponseEntity;
import de.fearnixx.jeak.service.controller.RequestMethod;
import de.fearnixx.jeak.service.controller.ResponseEntity;

@RestController(pluginId = "testPluginId", endpoint = "/test")
public class TestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "/hello")
    public ResponseEntity<DummyObject> hello() {
        ResponseEntity<DummyObject> responseEntity = new ResponseEntity<>(new DummyObject("Finn", 20));
        responseEntity.addHeader("Cache-Control", "max-age=0");
        return responseEntity;
    }

    @RequestMapping(method =  RequestMethod.GET, endpoint = "/info")
    public String returnSentInfo(@RequestParam(name = "name") String name) {
        return "received " + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody(type = String.class) String string) {
        return "this is the body " + string;
    }

    public IResponseEntity<String> hallo() {
        ResponseEntity<String> stringResponseEntity = new ResponseEntity<>("");
        stringResponseEntity.addHeader("some-header", "GET");
        return stringResponseEntity;
    }
}
