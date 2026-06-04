package com.echo.timeline.application;

import com.echo.timeline.domain.FeedItem;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 피드 조립 서비스.
 *
 * <p>앱 계층이 넘긴 후보 글들을 최신순으로 조립해 돌려준다.
 * post 모듈에 직접 의존하지 않고, 중립 타입 {@link FeedItem}만 다룬다.
 */
@Service
public class TimelineService {

    /**
     * 후보 글들을 최신순(createdAt 내림차순)으로 조립한다.
     *
     * <p>TODO(후속 챕터): limit/페이지네이션, 팔로우 그래프 필터, 랭킹.
     */
    public List<FeedItem> assembleLatest(List<FeedItem> candidates) {
        return candidates.stream()
                .sorted(Comparator.comparing(FeedItem::createdAt).reversed())
                .toList();
    }
}
