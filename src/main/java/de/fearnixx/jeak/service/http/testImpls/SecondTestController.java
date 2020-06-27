package de.fearnixx.jeak.service.http.testImpls;

import de.fearnixx.jeak.reflect.http.RequestBody;
import de.fearnixx.jeak.reflect.http.RequestMapping;
import de.fearnixx.jeak.reflect.http.RequestParam;
import de.fearnixx.jeak.reflect.http.RestController;
import de.fearnixx.jeak.service.http.RequestMethod;

@RestController(endpoint = "/test", pluginId = "testPluginId")
public class SecondTestController {

    @RequestMapping(method = RequestMethod.GET, endpoint = "/hello")
    public DummyObject hello() {
        return new DummyObject("second", 20);
    }

    @RequestMapping(method =  RequestMethod.GET, endpoint = "/info/:name")
    public String returnSentInfo(@RequestParam(name = "name") String name) {
        return "second" + name;
    }

    @RequestMapping(method = RequestMethod.POST, endpoint = "/body")
    public String sendBody(@RequestBody() String string) {
        return "second body " + string;
    }
}
