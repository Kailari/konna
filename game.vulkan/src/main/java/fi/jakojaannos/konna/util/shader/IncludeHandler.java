package fi.jakojaannos.konna.util.shader;

import org.lwjgl.util.shaderc.ShadercIncludeResolve;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.*;

final class IncludeHandler {
    static final class Resolver extends ShadercIncludeResolve {
        private final ByteBuffer sourceBuffer;
        private final String path;

        Resolver(final ByteBuffer sourceBuffer, final String path) {
            this.sourceBuffer = sourceBuffer;
            this.path = path;
        }

        @Override
        public long invoke(
                final long userData,
                final long requestedSource,
                final int type,
                final long requestingSource,
                final long includeDepth
        ) {
            final var includeResult = ShadercIncludeResult.calloc();
            final var pathRelativeToCurrentFile = this.path.substring(0, this.path.lastIndexOf('/')) + '/';
            final var requestedFilePath = memUTF8(requestedSource);
            final var sourcePath = pathRelativeToCurrentFile + requestedFilePath;

            try {
                final var bytes = Files.readAllBytes(Paths.get(sourcePath));
                final var byteBuffer = memAlloc(bytes.length);
                byteBuffer.put(0, bytes);
                includeResult.content(byteBuffer);
                includeResult.source_name(memUTF8(sourcePath));
                return includeResult.address();
            } catch (final IOException e) {
                throw new AssertionError("Failed to resolve include: " + this.sourceBuffer);
            }
        }
    }

    static final class Releaser extends ShadercIncludeResultRelease {
        public void invoke(final long userData, final long includeResult) {
            final var result = ShadercIncludeResult.create(includeResult);
            memFree(result.source_name());
            result.free();
        }
    }
}