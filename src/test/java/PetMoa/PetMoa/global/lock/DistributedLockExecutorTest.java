package PetMoa.PetMoa.global.lock;

import PetMoa.PetMoa.global.exception.LockAcquisitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class DistributedLockExecutorTest {

    private RedissonClient redissonClient;
    private RLock lock;
    private DistributedLockExecutor executor;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        lock = mock(RLock.class);
        executor = new DistributedLockExecutor(redissonClient);
    }

    @Test
    @DisplayName("락 획득 성공 시 작업을 실행하고 결과를 반환한다")
    void executeWithLock_success() throws InterruptedException {
        // given
        String lockKey = "lock:timeslot:1";
        given(redissonClient.getLock(lockKey)).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
        given(lock.isHeldByCurrentThread()).willReturn(true);

        // when
        String result = executor.executeWithLock(lockKey, () -> "success");

        // then
        assertThat(result).isEqualTo("success");
        verify(lock).unlock();
    }

    @Test
    @DisplayName("락 획득 실패 시 LockAcquisitionException을 던진다")
    void executeWithLock_failToAcquire() throws InterruptedException {
        // given
        String lockKey = "lock:timeslot:1";
        given(redissonClient.getLock(lockKey)).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> executor.executeWithLock(lockKey, () -> "success"))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessageContaining("현재 요청이 많아");
    }

    @Test
    @DisplayName("인터럽트 발생 시 LockAcquisitionException을 던진다")
    void executeWithLock_interrupted() throws InterruptedException {
        // given
        String lockKey = "lock:timeslot:1";
        given(redissonClient.getLock(lockKey)).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS)))
                .willThrow(new InterruptedException());

        // when & then
        assertThatThrownBy(() -> executor.executeWithLock(lockKey, () -> "success"))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessageContaining("인터럽트");
    }

    @Test
    @DisplayName("비즈니스 로직 예외 발생 시에도 락은 해제된다")
    void executeWithLock_exceptionInSupplier_stillUnlocks() throws InterruptedException {
        // given
        String lockKey = "lock:timeslot:1";
        given(redissonClient.getLock(lockKey)).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).willReturn(true);
        given(lock.isHeldByCurrentThread()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> executor.executeWithLock(lockKey, () -> {
            throw new RuntimeException("비즈니스 예외");
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("비즈니스 예외");

        verify(lock).unlock();
    }
}
