package br.com.caelum.vraptor.security.interceptor;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.security.AuthorizationRestrictionListener;
import br.com.caelum.vraptor.security.annotation.Secured;

@Interceptor
@Secured
public class SecurityInterceptor extends
		AnnotationsAuthorizingMethodInterceptor {

	@Inject
	private AuthorizationRestrictionListener listener;

	private static final Logger log = LoggerFactory
			.getLogger(SecurityInterceptor.class);

	@AroundInvoke
	public Object check(InvocationContext ctx) throws Exception {
		try {
			log.info("Checking permissions on method [{}]", ctx.getMethod().getName());
			assertAuthorized(new InvocationContextToMethodInvocationConverter(
					ctx));
		} catch (AuthorizationException e) {
			log.info("Permission denied.");
			listener.onAuthorizationRestriction(e);
		}
		return ctx.proceed();
	}

	private static class InvocationContextToMethodInvocationConverter implements
			MethodInvocation {
		private final InvocationContext context;

		public InvocationContextToMethodInvocationConverter(
				InvocationContext ctx) {
			context = ctx;
		}

		public Object proceed() throws Throwable {
			return context.proceed();
		}

		public Method getMethod() {
			return context.getMethod();
		}

		public Object[] getArguments() {
			return context.getParameters();
		}

		public Object getThis() {
			return context.getTarget();
		}
	}
}
