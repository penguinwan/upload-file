@Grab(group='org.apache.camel', module='camel-core', version='2.20.2')
@Grab(group='org.apache.camel', module='camel-netty4', version='2.20.2')
@Grab(group='org.apache.camel', module='camel-netty4-http', version='2.20.2')
@Grab(group='org.apache.camel', module='camel-groovy', version='2.20.2')
@Grab('org.slf4j:slf4j-simple:1.6.6')

import io.netty.handler.codec.http.multipart.*;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty4.http.NettyHttpMessage;
import org.apache.camel.impl.DefaultCamelContext;


def camelContext = new DefaultCamelContext()
camelContext.addRoutes(new RouteBuilder() {
    def void configure() {
        from('netty4-http:http://localhost:9080/hello')
        .process { 
			println "headers ${it.in.headers}"
			HttpPostRequestDecoder request = new HttpPostRequestDecoder(it.getIn(NettyHttpMessage.class).getHttpRequest());
			try {
				for (InterfaceHttpData part : request.getBodyHttpDatas()) {
					if (part instanceof MixedAttribute) {
						Attribute attribute = (MixedAttribute) part;
						println "found key[${attribute.getName()}] value[${attribute.getValue()}]"
					} else if (part instanceof MixedFileUpload) {
						MixedFileUpload attribute = (MixedFileUpload) part;
						println "found key[${attribute.getName()}] value[${attribute.getFilename()}] "
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				request.destroy();
			}
			it.in.body = 'ok'
		}
	}
})
camelContext.start()

addShutdownHook{ camelContext.stop() }
synchronized(this){ this.wait() }