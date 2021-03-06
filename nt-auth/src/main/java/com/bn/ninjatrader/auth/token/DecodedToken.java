package com.bn.ninjatrader.auth.token;

import com.bn.ninjatrader.common.type.Role;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;

/**
 * @author bradwee2000@gmail.com
 */
public class DecodedToken {

  public static final Builder builder() {
    return new Builder();
  }

  private final String tokenId;
  private final String userId;
  private final String firstName;
  private final String lastName;
  private final Collection<Role> roles;

  public DecodedToken(final String tokenId,
                      final String userId,
                      final String firstName,
                      final String lastName,
                      final Collection<Role> roles) {
    this.tokenId = tokenId;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.roles = Collections.unmodifiableCollection(roles);
  }

  public String getTokenId() {
    return tokenId;
  }

  public String getUserId() {
    return userId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Collection<Role> getRoles() {
    return roles;
  }

  public boolean hasRole(final String roleId) {
    for (final Role role : roles) {
      if (role.getId().equals(roleId)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasRole(final Role role) {
    if (role == null) return false;
    return roles.contains(role);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DecodedToken that = (DecodedToken) o;
    return Objects.equal(tokenId, that.tokenId) &&
        Objects.equal(userId, that.userId) &&
        Objects.equal(firstName, that.firstName) &&
        Objects.equal(lastName, that.lastName) &&
        Objects.equal(roles, that.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(tokenId, userId, firstName, lastName, roles);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tokenId", tokenId)
        .add("userId", userId)
        .add("firstName", firstName)
        .add("lastName", lastName)
        .add("roles", roles)
        .toString();
  }

  /**
   * Builder
   */
  public static final class Builder {
    private String tokenId;
    private String userId;
    private String firstName;
    private String lastName;
    private Collection<Role> roles = Lists.newArrayList();

    public Builder tokenId(final String tokenId) {
      this.tokenId = tokenId;
      return this;
    }

    public Builder userId(final String userId) {
      this.userId = userId;
      return this;
    }

    public Builder firstName(final String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(final String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder addRoleIds(final Collection<String> roles) {
      if (roles == null) return this;
      for (String role :roles) {
        this.roles.add(Role.findById(role));
      }
      return this;
    }

    public Builder addRole(final Role role) {
      this.roles.add(role);
      return this;
    }

    public Builder addRoles(final Role role, final Role ... more) {
      this.roles.addAll(Lists.asList(role, more));
      return this;
    }

    public Builder addRoles(final Collection<Role> roles) {
      if (roles != null) {
        this.roles.addAll(roles);
      }
      return this;
    }

    public DecodedToken build() {
      return new DecodedToken(tokenId, userId, firstName, lastName, roles);
    }
  }
}
