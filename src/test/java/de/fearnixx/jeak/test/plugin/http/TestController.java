package de.fearnixx.jeak.test.plugin.http;

import de.fearnixx.jeak.reflect.http.RequestMapping;
import de.fearnixx.jeak.reflect.http.RestController;
import de.fearnixx.jeak.reflect.http.params.QueryParam;
import de.fearnixx.jeak.reflect.http.params.RequestBody;
import de.fearnixx.jeak.service.http.IResponseEntity;
import de.fearnixx.jeak.service.http.RequestMethod;
import de.fearnixx.jeak.service.http.ResponseEntity;
import org.eclipse.jetty.http.HttpStatus;

@RestController(pluginId = "testPluginId", path = "/test")
public class TestController {

    @RequestMapping(endpoint = "/hello")
    public ResponseEntity<DummyObject> hello() {
        return new ResponseEntity.Builder<>(new DummyObject("Finn", 20))
                .withHeader("Cache-Control", "max-age=0")
                .withStatus(HttpStatus.OK_200)
                .build();
    }

    @RequestMapping(endpoint = "/info")
    public String returnSentInfo(@QueryParam(name = "name") String name) {
        return "received " + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody String string) {
        return "this is the body " + string;
    }

    @RequestMapping(endpoint = "/int")
    public String sendStuff(@QueryParam(name = "num") Integer num) {
        return "received" + num;
    }

    @RequestMapping(endpoint = "/hallo")
    public IResponseEntity<String> hallo() {
        return new ResponseEntity.Builder<String>()
                .withHeader("some-header", "GET")
                .build();
    }
}
