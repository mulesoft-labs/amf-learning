package org.mulesoft.amf.amc;

import amf.client.model.document.DialectInstance;
import amf.plugins.features.validation.JenaRdfModel;
import org.mulesoft.amf.util.AmlOps;
import org.mulesoft.amf.util.Instance;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ConfigurationJourney implements AmlOps {

    CompletableFuture<Stream<ConfSecret>> listSecrets(Instance instance) {
        return parseAmlUnit(instance).thenApply(this::toNative).thenCompose(model -> {
            final String query = readUTF8("queries/secrets.sparql");
            return querySelect(model, query, true).thenApply(stream -> stream.map(ConfSecret::create));
        });
    }

    CompletableFuture<DialectInstance> updateSecrets(Instance instance, Stream<ConfSecret> secrets) {
        return parseAmlUnit(instance).thenApply(unit -> {
            final JenaRdfModel model = toNative(unit);
            final String secretize = QueryFactory.updateSecrets(secrets);
            queryUpdate(model, secretize);
            return fromNative(unit.id(), model);
        });
    }

    @Override public Instance vocabulary() { return Instance.createVocabulary("vocabularies/amc.yaml"); }

    @Override public Instance dialect() { return Instance.createDialect("dialect/mule_application_dialect.yaml"); }
}
