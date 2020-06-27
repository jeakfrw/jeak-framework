package de.fearnixx.jeak.service.http.request;

import de.fearnixx.jeak.service.http.request.token.IAuthenticationToken;

import java.util.Optional;

public interface IRequestContext {

    /**
     * Optionally retrieves a request attribute from the context.
     * For officially supported attributes, see {@link Attributes}.
     */
    <T> Optional<T> optAttribute(String name, Class<T> hint);

    final class Attributes {

        /**
         * {@link IRequestContext}
         *
         * @apiNote Self-reference, mainly for internal purposes.
         */
        public static final String REQUEST_CONTEXT = "self";

        /**
         * {@link IAuthenticationToken}
         *
         * @apiNote Optionally filled, if authentication is successful AND the "Token" authentication scheme is used.
         *
         * @implNote Required parameter injections cause {@link org.eclipse.jetty.http.HttpStatus#UNAUTHORIZED_401} on unsuccessful authentication.
         */
        public static final String AUTHENTICATION_TOKEN = "auth:token:authenticationToken";

        /**
         * {@link de.fearnixx.jeak.teamspeak.data.IUser}
         *
         * @apiNote Optionally filled, if authentication is successful AND the subject is an user.
         * @implNote Required parameter injections cause {@link org.eclipse.jetty.http.HttpStatus#UNAUTHORIZED_401} on unsuccessful authentication or {@link org.eclipse.jetty.http.HttpStatus#FORBIDDEN_403} for principals that aren't users.
         */
        public static final String AUTHENTICATION_USER = "auth:subject:authenticatedUser";

        private Attributes() {
        }
    }
}
