package de.fearnixx.jeak.service.http.testImpls;

import de.fearnixx.jeak.reflect.http.RequestBody;
import de.fearnixx.jeak.reflect.http.RequestMapping;
import de.fearnixx.jeak.reflect.http.RequestParam;
import de.fearnixx.jeak.reflect.http.RestController;
import de.fearnixx.jeak.service.http.IResponseEntity;
import de.fearnixx.jeak.service.http.RequestMethod;
import de.fearnixx.jeak.service.http.ResponseEntity;
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
    public String sendBody(@RequestBody() String string) {
        return "this is the body " + string;
    }

    public IResponseEntity<String> hallo() {
        return new ResponseEntity.Builder<String>()
                .withHeader("some-header", "GET")
                .build();
    }
}
