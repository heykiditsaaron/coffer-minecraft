package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CofferMinecraftFabricMutationContactCarrierTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;
    @TempDir
    Path tempDir;

    @Test
    void inertCarrierRepresentsContactAndNoContactWithoutSuccessClaims() {
        InertMutationContactCarrier noContact = new InertMutationContactCarrier(MutationBoundaryContact.NO_CONTACT);
        InertMutationContactCarrier contacted = new InertMutationContactCarrier(MutationBoundaryContact.CONTACTED);

        assertFalse(noContact.hasMutationBoundaryContact());
        assertTrue(contacted.hasMutationBoundaryContact());
        assertFalse(noContact.claimsMutationSuccess());
        assertFalse(contacted.claimsMutationSuccess());
        assertEquals(1, InertMutationContactCarrier.class.getDeclaredFields().length);
        assertEquals(MutationBoundaryContact.class, InertMutationContactCarrier.class.getDeclaredFields()[0].getType());
    }

    @Test
    void inertCarrierDoesNotCarryPlayerCommandUxOrInventoryState() {
        assertEquals(1, InertMutationContactCarrier.class.getDeclaredFields().length);
        assertEquals(MutationBoundaryContact.NO_CONTACT, new InertMutationContactCarrier(MutationBoundaryContact.NO_CONTACT).contact());
        assertEquals(MutationBoundaryContact.CONTACTED, new InertMutationContactCarrier(MutationBoundaryContact.CONTACTED).contact());
    }

    @Test
    void contactedCarrierStillRefusesToClaimMutationSuccess() {
        InertMutationContactCarrier contacted = new InertMutationContactCarrier(MutationBoundaryContact.CONTACTED);

        assertTrue(contacted.hasMutationBoundaryContact());
        assertFalse(contacted.claimsMutationSuccess());
    }

    @Test
    void currentAccountabilityRecordsStillDoNotEmitMutationSeam() throws IOException {
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "carrier-test", () -> TIMESTAMP);

        accountability.recordServerStarted(tempDir);
        accountability.recordConstructionRefused(tempDir, "MISSING_BINDING_ID");
        accountability.recordCoreDenied(tempDir, "VALUE_NOT_REMOVABLE");
        accountability.recordCoreApproved(tempDir);
        accountability.recordRuntimeUnknown(tempDir, "MALFORMED_RUNTIME_DESCRIPTOR");

        List<String> lines = Files.readAllLines(accountability.logPath(tempDir));

        assertEquals(
                List.of(
                        "{\"timestamp\":1700000000000,\"interactionId\":\"carrier-test\",\"recordType\":\"SER\",\"stage\":\"fabric_server_started\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"carrier-test\",\"recordType\":\"SER\",\"stage\":\"fabric_construction_refused\",\"code\":\"MISSING_BINDING_ID\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"carrier-test\",\"recordType\":\"CER\",\"stage\":\"fabric_core_denied\",\"seam\":\"fabric_core\",\"code\":\"VALUE_NOT_REMOVABLE\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"carrier-test\",\"recordType\":\"CER\",\"stage\":\"fabric_core_approved\",\"seam\":\"fabric_core\"}",
                        "{\"timestamp\":1700000000000,\"interactionId\":\"carrier-test\",\"recordType\":\"CER\",\"stage\":\"fabric_runtime_unknown\",\"seam\":\"fabric_runtime\",\"code\":\"MALFORMED_RUNTIME_DESCRIPTOR\"}"),
                lines);
        assertTrue(lines.stream().noneMatch(line -> line.contains("fabric_mutation")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"player\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"inventory\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"command\"")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("\"ux\"")));
    }

    private enum MutationBoundaryContact {
        NO_CONTACT,
        CONTACTED
    }

    private record InertMutationContactCarrier(MutationBoundaryContact contact) {
        boolean hasMutationBoundaryContact() {
            return contact == MutationBoundaryContact.CONTACTED;
        }

        boolean claimsMutationSuccess() {
            return false;
        }
    }
}
