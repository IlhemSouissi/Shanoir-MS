package org.shanoir.ng.model.validation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.shanoir.ng.model.Role;
import org.shanoir.ng.model.error.FieldError;
import org.shanoir.ng.model.error.FieldErrorMap;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

public class EditableOnlyByValidator<T> {

	private static final Logger LOG = LoggerFactory.getLogger(EditableOnlyByValidator.class);

	/**
	 * Validates an update
	 *
	 * @param user
	 * @return the forgotten fields names
	 */
	public FieldErrorMap validate(final T originalEntity, final T editedEntity) {
		final Collection<? extends GrantedAuthority> connectedUserRoles = getConnectedUserRoles();
		final FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (final Field field : originalEntity.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(EditableOnlyBy.class)) {
					final EditableOnlyBy annotation = field.getAnnotation(EditableOnlyBy.class);
					final String getterName = "get" + StringUtils.capitalize(field.getName());
					try {
						final Method originalGetter = originalEntity.getClass().getMethod(getterName);
						final Method editedGetter = editedEntity.getClass().getMethod(getterName);
						final Object originalValue = originalGetter.invoke(originalEntity);
						final Object givenValue = editedGetter.invoke(editedEntity);
						final boolean fieldHasBeenModified = !Utils.equalsIgnoreNull(originalValue, givenValue);
						if (fieldHasBeenModified && !haveOneRoleInCommon(annotation.roles(), connectedUserRoles)) {
							final List<FieldError> errors = new ArrayList<FieldError>();
							errors.add(new FieldError("unauthorized", "You do not have the right to edit this field",
									givenValue));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @EditableOnlyBy custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error(
								"Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
										+ getterName + "() for accessing " + originalEntity.getClass().getName() + "."
										+ field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @EditableOnlyBy custom annotation", e);
		}
		return errorMap;
	}

	/**
	 * Validates a creation
	 *
	 * @param user
	 * @return the forgotten fields names
	 */
	public FieldErrorMap validate(final T editedEntity) {
		final Collection<? extends GrantedAuthority> connectedUserRoles = getConnectedUserRoles();
		final FieldErrorMap errorMap = new FieldErrorMap();
		try {
			for (final Field field : editedEntity.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(EditableOnlyBy.class)) {
					final EditableOnlyBy annotation = field.getAnnotation(EditableOnlyBy.class);
					final String getterName = "get" + StringUtils.capitalize(field.getName());
					try {
						final Method editedGetter = editedEntity.getClass().getMethod(getterName);
						final Object givenValue = editedGetter.invoke(editedEntity);
						if (givenValue != null && !haveOneRoleInCommon(annotation.roles(), connectedUserRoles)) {
							final List<FieldError> errors = new ArrayList<FieldError>();
							errors.add(new FieldError("unauthorized", "You do not have the right to edit this field",
									givenValue));
							errorMap.put(field.getName(), errors);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
						LOG.error("Error while checking @EditableOnlyBy custom annotation", e);
					} catch (NoSuchMethodException e) {
						LOG.error(
								"Error while checking @EditableOnlyBy custom annotation, you must implement a method named "
										+ getterName + "() for accessing " + editedEntity.getClass().getName() + "."
										+ field.getName());
					}
				}
			}
		} catch (SecurityException e) {
			LOG.error("Error while checking @EditableOnlyBy custom annotation", e);
		}
		return errorMap;
	}

	private boolean haveOneRoleInCommon(final String[] roles,
			final Collection<? extends GrantedAuthority> authorities) {
		for (final String role : roles) {
			for (final GrantedAuthority authority : authorities) {
				if (role != null && role.equals(authority.getAuthority())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get connected user roles. If anonymous user, returns an empty list.
	 * 
	 * @return roles
	 */
	private Collection<? extends GrantedAuthority> getConnectedUserRoles() {
		Collection<? extends GrantedAuthority> connectedUserRoles;
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			connectedUserRoles = new ArrayList<Role>();
		} else {
			UserDetails connectedUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			if (connectedUser == null) {
				throw new IllegalArgumentException("connectedUser cannot be null");
			}
			connectedUserRoles = connectedUser.getAuthorities();
		}
		return connectedUserRoles;
	}

}
