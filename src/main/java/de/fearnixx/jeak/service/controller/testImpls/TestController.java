package de.fearnixx.jeak.service.controller.testImpls;

import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.controller.IResponseEntity;
import de.fearnixx.jeak.service.controller.RequestMethod;
import de.fearnixx.jeak.service.controller.ResponseEntity;
import org.eclipse.jetty.http.HttpStatus;

@RestController(pluginId = "testPluginId", endpoint = "/test")
public class TestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "/hello")
    public ResponseEntity<DummyObject> hello() {
        return new ResponseEntity.Builder<DummyObject>(new DummyObject("Finn", 20))
                .withHeader("Cache-Control", "max-age=0")
                .withStatus(HttpStatus.OK_200)
                .build();
    }

    @RequestMapping(method = RequestMethod.GET, endpoint = "/info")
    public String returnSentInfo(@RequestParam(name = "name") String name) {
        return "received " + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody String string) {
        return "this is the body " + string;
    }

    @RequestMapping(method = RequestMethod.GET, endpoint = "/int", isSecured = false)
    public String sendStuff(@RequestParam(name = "num", type = Integer.class) Integer num) {
        return "received" + num;
    }

    public IResponseEntity<String> hallo() {
        return new ResponseEntity.Builder<String>()
                .withHeader("some-header", "GET")
                .build();
    }
}
