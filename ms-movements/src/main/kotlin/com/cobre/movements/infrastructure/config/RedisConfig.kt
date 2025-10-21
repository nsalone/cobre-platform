import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val connectionFactory: ReactiveRedisConnectionFactory
) {
    @Bean
    fun reactiveRedisTemplateString(): ReactiveRedisTemplate<String, String> {
        val stringSer = StringRedisSerializer()

        val context = RedisSerializationContext
            .newSerializationContext<String, String>(stringSer)
            .key(stringSer)
            .value(stringSer)
            .hashKey(stringSer)
            .hashValue(stringSer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, context)
    }
}
