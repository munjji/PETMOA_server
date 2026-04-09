package PetMoa.PetMoa.global.lock;

import PetMoa.PetMoa.global.exception.LockAcquisitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

    private final RedissonClient redissonClient;

    private static final long DEFAULT_WAIT_TIME = 3L;
    private static final long DEFAULT_LEASE_TIME = 5L;

    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, supplier);
    }

    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("락 획득 실패: {}", lockKey);
                throw new LockAcquisitionException("현재 요청이 많아 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }

            log.debug("락 획득 성공: {}", lockKey);
            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("락 해제: {}", lockKey);
            }
        }
    }
}
