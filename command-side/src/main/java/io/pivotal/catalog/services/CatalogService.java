package io.pivotal.catalog.services;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pivotal.catalog.commands.AddProductToCatalogCommand;
import io.pivotal.catalog.configuration.AmqpEventPublicationConfiguration;

@Service
public class CatalogService{

    private static final Logger LOG = LoggerFactory.getLogger(CatalogService.class);

    private final CommandGateway commandGateway;

    public CatalogService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
	private RabbitTemplate rabbitTemplate;
    
    public CompletableFuture<String> addProductToCatalog(AddProductToCatalogCommand command) {
        LOG.info("Processing AddProductToCatalogCommand command: {}", command + " Id : "+ command.getId() + " Name : "+ command.getName());
        
        
        rabbitTemplate.convertAndSend(AmqpEventPublicationConfiguration.exchangeName,AmqpEventPublicationConfiguration.ROUTING_KEY,command);
        LOG.info(" post  convertAndSend .....");
        
        return this.commandGateway.send(command);
    }
}
