package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadInterpreter;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimeValueSetResolver;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.coffer.core.authority.AuthorityResolver;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeAuthority;
import org.coffer.runtime.CofferRuntime;
import org.coffer.runtime.authority.RuntimeAuthority;

final class CofferMinecraftFabricService {
    private static final System.Logger LOGGER =
            System.getLogger(CofferMinecraftFabricService.class.getName());

    private final CofferRuntime cofferRuntime;
    private final TransferableValueCoreAuthority transferableValueCoreAuthority;
    private final TransferableValueRuntimeAuthority transferableValueRuntimeAuthority;
    private final AuthorityResolver coreAuthorityResolver;
    private final List<RuntimeAuthority> runtimeAuthorities;
    private boolean initialized;

    CofferMinecraftFabricService() {
        MinecraftContainerResolver containerResolver =
                new MinecraftContainerResolver((playerId, region) -> Optional.empty());
        MinecraftDescriptorFactory descriptorFactory = new MinecraftDescriptorFactory();
        MinecraftRuntimePayloadFactory runtimePayloadFactory = new MinecraftRuntimePayloadFactory();
        MinecraftRuntimeValueSetResolver runtimeValueSetResolver = new MinecraftRuntimeValueSetResolver();
        MinecraftRuntimePayloadInterpreter runtimePayloadInterpreter = new MinecraftRuntimePayloadInterpreter();

        this.transferableValueCoreAuthority = new TransferableValueCoreAuthority(
                containerResolver,
                descriptorFactory,
                runtimePayloadFactory);
        this.transferableValueRuntimeAuthority = new TransferableValueRuntimeAuthority(
                containerResolver,
                runtimeValueSetResolver,
                runtimePayloadInterpreter,
                reasonCode -> new OpaqueObject(Map.of("reasonCode", reasonCode)));
        this.cofferRuntime = new CofferRuntime();
        this.coreAuthorityResolver = authority -> {
            if (transferableValueCoreAuthority.identifiers().contains(authority)) {
                return new ResolutionResult.Resolved(transferableValueCoreAuthority);
            }
            return new ResolutionResult.Unresolved();
        };
        this.runtimeAuthorities = List.of(transferableValueRuntimeAuthority);
    }

    void initialize() {
        initialized = true;
        LOGGER.log(System.Logger.Level.INFO, "Coffer Fabric platform initialized");
        LOGGER.log(System.Logger.Level.INFO, "Coffer authorities wired");
    }

    void shutdown() {
        initialized = false;
    }

    boolean initialized() {
        return initialized;
    }
}
