package com.radyfy.common.config.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.radyfy.common.commons.Constants;
import com.radyfy.common.commons.RoleType;
import com.radyfy.common.model.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user, String accountId) {
        List<GrantedAuthority> roles = new ArrayList<>();

        /**
         * The first entry in the authorities list will be the user's parent account ID.
         * This value is used when retrieving documents associated with this account
         */
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(String.valueOf(user.getId()));
        roles.add(authority);

        if(Constants.RADYFY_ACCOUNT_ID.equals(accountId)){
            if(RoleType.SUPER_ADMIN.equals(user.getAppRoleId())){
                roles.add(new SimpleGrantedAuthority("ROLE_" + RoleType.SUPER_ADMIN));
            } else if(RoleType.SYSTEM_ADMIN.equals(user.getAppRoleId())){
                roles.add(new SimpleGrantedAuthority("ROLE_" + RoleType.SYSTEM_ADMIN));
            }
        } else {
            if(RoleType.ACCOUNT_ADMIN.equals(user.getAppRoleId())){
                roles.add(new SimpleGrantedAuthority("ROLE_" + RoleType.ACCOUNT_ADMIN));
            }
        }

        /**
         * We will check for specific roles on resources to verify the user's
         * permissions.
         */
        // permissions.forEach(role -> {
        //     SimpleGrantedAuthority a = new SimpleGrantedAuthority(role);
        //     if (!roles.contains(a)) {
        //         roles.add(a);
        //     }
        // });

        return new UserDetailsImpl(user, roles);
    }

    public String getId() {
        return user.getId();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(user.getStatus());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
