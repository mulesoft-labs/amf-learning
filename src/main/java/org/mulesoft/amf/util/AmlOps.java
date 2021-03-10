package org.mulesoft.amf.util;

import amf.Core;
import amf.MessageStyle;
import amf.ProfileName;
import amf.client.AMF;
import amf.client.model.document.BaseUnit;
import amf.client.model.document.DialectInstance;
import amf.client.model.domain.DialectDomainElement;
import amf.client.parse.Aml10Parser;
import amf.client.parse.DefaultParserErrorHandler;
import amf.client.render.Aml10Renderer;
import amf.client.render.RenderOptions;
import amf.core.parser.EmptyFutureDeclarations;
import amf.core.parser.ParsedReference;
import amf.core.parser.ParserContext;
import amf.core.plugin.PluginContext;
import amf.plugins.features.validation.JenaRdfModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.update.UpdateAction;
import scala.collection.Seq;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static amf.core.model.document.BaseUnit.fromNativeRdfModel;
import static scala.collection.JavaConverters.asScalaBuffer;

public interface AmlOps extends IOOps {

    Instance vocabulary();
    Instance dialect();

    default CompletableFuture<DialectDomainElement> parseAml(Instance instance) {
        return parseAmlUnit(instance).thenApply(u -> ((DialectInstance) u).encodes());
    }

    default CompletableFuture<BaseUnit> parseAmlUnit(Instance instance) {
        final Instance vocabulary = vocabulary();
        final Instance dialect = dialect();
        return parseAml(vocabulary.getUri(), vocabulary.getDialect(), vocabulary.getMediaType())
                .thenCompose(v -> parseAml(dialect.getUri(), dialect.getDialect(), dialect.getMediaType()))
                .thenCompose(d -> parseAml(instance.getUri(), instance.getDialect(), instance.getMediaType()));
    }

    default CompletableFuture<BaseUnit> parseAml(String uri, String dialect, String mediaType) {
        return init()
                .thenCompose(init -> parse(uri, mediaType))
                .thenCompose(unit -> validate(unit, dialect));
    }

    default CompletableFuture<BaseUnit> parse(String uri, String mediaType) {
        final Aml10Parser parser = new Aml10Parser(mediaType);
        return parser.parseFileAsync(uri);
    }

    default CompletableFuture<BaseUnit> validate(BaseUnit unit, String dialect) {
        return Core.validate(unit, ProfileName.apply(dialect), MessageStyle.apply("RAML")).thenApply(report -> {
            if(!report.conforms()) throw new IllegalStateException(report.toString());
            return unit;
        });
    }

    default CompletableFuture<String> renderAml(BaseUnit unit, String mediaType) {
        return new Aml10Renderer(mediaType).generateString(unit);
    }

    default JenaRdfModel toNative(BaseUnit unit) { return ((JenaRdfModel) unit.toNativeRdfModel(new RenderOptions())); }

    default DialectInstance fromNative(String id, JenaRdfModel model) {
        final Seq<ParsedReference> stub = asScalaBuffer(Collections.<ParsedReference>emptyList()).toSeq();
        final amf.core.model.document.BaseUnit unit =
                fromNativeRdfModel(id, model, ParserContext.apply("", stub, EmptyFutureDeclarations.apply(), DefaultParserErrorHandler.apply(), PluginContext.apply()));
        return new DialectInstance((amf.plugins.document.vocabularies.model.document.DialectInstance) unit);
    }

    default CompletableFuture<Stream<QuerySolution>> querySelect(JenaRdfModel model, String sparql) {
        final Query query = QueryFactory.create(sparql);

        return parseAmlUnit(vocabulary()).thenApply(this::toNative).thenApply(v -> {

            final Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(v.model());
            final InfModel m = ModelFactory.createInfModel(reasoner, model.model());

            try (QueryExecution execution = QueryExecutionFactory.create(query, m)) {
                final ResultSet rs = execution.execSelect();
                final Iterable<QuerySolution> iterable = () -> rs;
                return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList()).stream();
            }
        });
    }

    default void queryUpdate(JenaRdfModel model, String sparql) {
        UpdateAction.parseExecute(sparql, model.model());
    }

    default CompletableFuture<?> init() {
        return AMF.init();
    }
}
