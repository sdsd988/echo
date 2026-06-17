package com.echo.api;

import com.echo.user.application.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 데모용 유저 시더. 유저 등록 API는 범위 외이므로, 앱 기동 시 유저가 없으면 데모 유저 몇 명을 만든다.
 * 덕분에 팔로우 엔드포인트를 Swagger 등에서 바로 시험해볼 수 있다(id=1,2,3).
 */
@Component
public class DemoUserSeeder implements CommandLineRunner {

    private final UserService userService;

    public DemoUserSeeder(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (userService.count() > 0) {
            return;
        }
        userService.create("alice", "Alice");
        userService.create("bob", "Bob");
        userService.create("carol", "Carol");
    }
}
