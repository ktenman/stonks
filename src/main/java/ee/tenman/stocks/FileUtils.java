package ee.tenman.stocks;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.util.stream.Collectors.joining;

public interface FileUtils {
	static String getSecret(final ClassPathResource classPathResource) {
		try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()))) {
			return buffer.lines().collect(joining(""));
		} catch (final IOException ignored) {
			return null;
		}
	}
}
