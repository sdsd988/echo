package com.echo.user.application;

import com.echo.user.domain.User;
import com.echo.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User 도메인 애플리케이션 서비스.
 *
 * <p>다른 모듈(예: follow)이 "대상 유저가 존재하는가"를 묻는 진입점이다.
 * 모듈 간 직접 의존을 피하기 위해, 이 검증은 조립 지점(컨트롤러)에서 호출한다.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 해당 id의 유저가 존재하는지. */
    public boolean exists(Long id) {
        return id != null && userRepository.existsById(id);
    }

    /** 등록된 유저 수. (데모 시드 여부 판단 등에 사용) */
    public long count() {
        return userRepository.count();
    }

    /** 유저를 생성한다. (유저 등록 API는 범위 외 — 데모 시드용) */
    @Transactional
    public User create(String username, String displayName) {
        return userRepository.save(new User(username, displayName));
    }
}
