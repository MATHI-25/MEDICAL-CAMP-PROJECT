package com.mediq.security;

import com.mediq.entity.User;
import com.mediq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        User user = userRepository.findByMemberIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with member ID: " + memberId));
        return new CustomUserDetails(user);
    }
}
