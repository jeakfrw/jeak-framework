package de.fearnixx.jeak.service.token;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * pluginId/
 * pluginId/Controller/
 * pluginId/Controller/method1
 */
public class TokenScope {
    private Set<String> scopeSet;

    public TokenScope(Set<String> scopeSet) {
        this.scopeSet = scopeSet;
    }

    public void addScope(String scope) {
        this.scopeSet.add(scope);
    }

    public Set<String> getScopeSet() {
        return scopeSet;
    }

    /**
     *
     * @param scope
     * @return
     */
    public boolean isInScope(String scope) {
        String[] splittedStrings = splitScope(scope);
        for (int i = 0; i < splittedStrings.length; i++) {
            Optional<String> optionalShortenedScope = combineStrings(Arrays.copyOf(splittedStrings, splittedStrings.length - i));
            if (optionalShortenedScope.isPresent()) {
                String localScope = optionalShortenedScope.get();
                if (scopeSet.contains(localScope)) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private Optional<String> combineStrings(String ... strings) {
        return Arrays.stream(strings)
                .reduce((s, s2) -> s+"/"+s2);
    }

    private String[] splitScope(String scope) {
        return scope.split("/");
    }
}
