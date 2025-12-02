local rateLimitKey = KEYS[1]
local attemptCountKey = KEYS[2]

--[[Does the rate limit key exist? If it does, get its TTL]]
if redis.call('EXISTS', rateLimitKey) == 1 then
    --[[TTL means "Time To Live" and returns the remaining time to live of a key that has a timeout]]
    local ttl = redis.call('TTL', rateLimitKey)
    --[[ Return -1 and the TTL if the key exists]]
    return {-1, ttl > 0 and ttl or 60}
end

--[[If the rate limit key does not exist, increment the attempt count]]
local currentCount = redis.call('GET', attemptCountKey)
currentCount = currentCount and tonumber(currentCount) or 0
local newCount = redis.call('INCR', attemptCountKey)

local backOfSeconds = { 60, 300, 600, 1800, 3600} -- 1 minuto, 5 minutos, 10 minutos, 30 minutos, 1 hora
local backoffIndex = math.min(newCount, 5)

redis.call('SETEX', rateLimitKey, backOfSeconds[backoffIndex], '1')
redis.call('EXPIRE', attemptCountKey, 86400) -- Set the attempt count key to expire in 24 hours

return { newCount, 0}
