package io.pivotal.catalog.configuration;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pivotal.catalog.aggregates.ProductAggregate;

@Configuration
@EnableScheduling
public class AmqpEventPublicationConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(ProductAggregate.class);

    //@Value("${axon.amqp.exchange}")
    //String exchangeName="CatalogEvents";
    
	public static final String exchangeName = "ProductEvents";
	
	public static final String ROUTING_KEY = "mqpox";
	
	public static final String QUEUE_SPECIFIC_NAME = "catalogEventsQueue";

    //    Add @Bean annotated exchange() method to declare a spring amqp Exchange
    //    Return the exchange from ExchangeBuilder.fanoutExchange(â€œComplaintEventsâ€�).build();

    @Bean
    public TopicExchange exchange(){
    	LOG.info("exchangeName before in exchange "+exchangeName);
        return new TopicExchange(exchangeName);
    }

    //    Add @Bean annotated queue() method to declate a Queue
    //    Return the queue from QueueBuilder.durable(â€œComplaintEventsâ€�).build()

    @Bean
    public Queue queue(){
    	LOG.info("exchangeName before queue"+QUEUE_SPECIFIC_NAME);
        return QueueBuilder.durable(QUEUE_SPECIFIC_NAME).build();
    }

    //    Add @Bean annotated binding() method to declare a Binding
    //    Return the binding from BindingBuilder.bind(queue()).to(exchange()).with(â€œ*â€�).noargs()

//    @Bean
//    public Binding binding(){
//        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
//    }
    @Bean
	public Binding declareBindingSpecific() {
		return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
	}

    //    Add @Autowired method to configure(AmqpAdmin admin)
    //    Make admin.declareExchange(exchange());
    //    Make admin.declareQueue(queue());
    //    Make admin.declareBinding(binding());

    @Autowired
    public void configure(AmqpAdmin amqpAdmin, Exchange exchange, Queue queue, Binding binding){
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);
        LOG.info("exchangeName after configuration "+exchangeName);
    }
    
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		
		rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
		
		return rabbitTemplate;
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter(ObjectMapper objectMapper) {
		DefaultClassMapper classMapper = new DefaultClassMapper();
		//classMapper.setTrustedPackages("*");
		classMapper.setDefaultType(Map.class);		
		Jackson2JsonMessageConverter converter = new ImplicitJsonMessageConverter(objectMapper);
		converter.setClassMapper(classMapper);
		return converter;
	}
	public static class ImplicitJsonMessageConverter extends Jackson2JsonMessageConverter {    
        public ImplicitJsonMessageConverter(ObjectMapper jsonObjectMapper) {
            super(jsonObjectMapper, "*");
        }    
        @Override
        public Object fromMessage(Message message) throws MessageConversionException {
            message.getMessageProperties().setContentType("application/json");
            return super.fromMessage(message);
        }
    }
}
