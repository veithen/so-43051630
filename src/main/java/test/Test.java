package test;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

public class Test {
    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);
        Server server = new Server(new QueuedThreadPool(4));
        
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8888);
        server.setConnectors(new Connector[] { connector });
        ServletContextHandler handler = new ServletContextHandler(server, "/");
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setContextClass(GenericWebApplicationContext.class);
        servlet.setContextInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            public void initialize(ConfigurableApplicationContext applicationContext) {
                XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((GenericWebApplicationContext)applicationContext);
                reader.loadBeanDefinitions(new ClassPathResource("server.xml", Test.class));
            }
        });
        ServletHolder servletHolder = new ServletHolder(servlet);
        servletHolder.setName("spring-ws");
        servletHolder.setInitOrder(1);
        handler.addServlet(servletHolder, "/*");

        server.start();
        
        StringBuilder buffer = new StringBuilder();
        for (int i=0; i<90000; i++) {
            for (int j=0; j<25; j++) {
                buffer.append("AzU9");
            }
            buffer.append((char)10);
        }
        ClientUploadRequest request = new ClientUploadRequest();
        request.setBytes(buffer.toString());
        GenericXmlApplicationContext context = new GenericXmlApplicationContext(new ClassPathResource("client.xml", Test.class));
        WebServiceTemplate ws = context.getBean(WebServiceTemplate.class);
        
        while (true) {
            long startTime = System.currentTimeMillis();

            System.out.println(((UploadResponse)ws.marshalSendAndReceive(request)).getBytes().length);
            
            System.out.println(System.currentTimeMillis()-startTime);
        }
        
//        server.stop();
    }
}
