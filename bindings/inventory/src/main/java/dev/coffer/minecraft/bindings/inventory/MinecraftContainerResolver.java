package dev.coffer.minecraft.bindings.inventory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import org.coffer.core.model.authority.AuthorityRequest;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueContainerResolver;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueContainer;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeContainerResolver;
import org.coffer.runtime.model.execution.MutationExecutionRequest;

public final class MinecraftContainerResolver
        implements TransferableValueContainerResolver, TransferableValueRuntimeContainerResolver {
    public static final String PLAYER_INVENTORY_KIND = "minecraft.player.inventory";

    private final PlayerInventorySlots slots;

    public MinecraftContainerResolver(PlayerInventorySlots slots) {
        this.slots = Objects.requireNonNull(slots, "slots");
    }

    @Override
    public Optional<TransferableValueContainer> resolve(
            ActorDeclaration actor,
            AuthorityRequest request,
            String bindingId) {
        Objects.requireNonNull(actor, "actor");
        if (!PLAYER_INVENTORY_KIND.equals(actor.kind())) {
            return Optional.empty();
        }
        return resolve(actor.actorRef());
    }

    @Override
    public Optional<TransferableValueContainer> resolve(
            ActorRef actorRef,
            String bindingId,
            Map<String, Object> runtimePayload,
            MutationExecutionRequest request) {
        Objects.requireNonNull(runtimePayload, "runtimePayload");
        return resolve(actorRef);
    }

    private Optional<TransferableValueContainer> resolve(ActorRef actorRef) {
        Objects.requireNonNull(actorRef, "actorRef");
        Optional<ContainerIdentity> identity = ContainerIdentity.parse(actorRef.value());
        if (identity.isEmpty()) {
            return Optional.empty();
        }

        ContainerIdentity parsed = identity.get();
        return Optional.of(new MinecraftPlayerInventoryContainer(
                actorRef.value(),
                parsed.region(),
                () -> slots.resolve(parsed.playerId(), parsed.region())));
    }

    @FunctionalInterface
    public interface PlayerInventorySlots {
        Optional<List<ItemStack>> resolve(UUID playerId, MinecraftPlayerInventoryContainer.Region region);
    }

    private record ContainerIdentity(UUID playerId, MinecraftPlayerInventoryContainer.Region region) {
        private static Optional<ContainerIdentity> parse(String actorRef) {
            String[] parts = actorRef.split(":");
            if (parts.length != 4 || !"player".equals(parts[0]) || !"inventory".equals(parts[2])) {
                return Optional.empty();
            }

            UUID playerId;
            try {
                playerId = UUID.fromString(parts[1]);
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }

            for (MinecraftPlayerInventoryContainer.Region region : MinecraftPlayerInventoryContainer.Region.values()) {
                if (region.serializedName().equals(parts[3])) {
                    return Optional.of(new ContainerIdentity(playerId, region));
                }
            }
            return Optional.empty();
        }
    }
}
