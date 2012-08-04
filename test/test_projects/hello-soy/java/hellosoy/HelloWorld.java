package hellosoy;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;

import java.io.File;

public class HelloWorld {

  public static void main (String[] args) {

    SoyFileSet sfs = new SoyFileSet.Builder()
        .add(new File("helloworld.soy")).build();
    SoyTofu tofu = sfs.compileToTofu();

    // Call the template with no data.
    System.out.println(tofu.newRenderer("examples.helloWorld").render());

    SoyTofu simpleTofu = tofu.forNamespace("examples");

    // Hello Name example
    System.out.println("--------------------");
    System.out.println(simpleTofu.newRenderer(".helloName")
        .setData(new SoyMapData("name", "Ana")).render());

    // Hello Names example
    System.out.println("--------------------");
    System.out.println(simpleTofu.newRenderer(".helloNames")
        .setData(new SoyMapData("name", "Ana",
            "additionalNames", new SoyListData("Bob", "Cid", "Dee")))
        .render());

  }

}