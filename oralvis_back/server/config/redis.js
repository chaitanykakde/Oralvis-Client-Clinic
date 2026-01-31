const Redis = require("ioredis");

const redisOptions = {
  host: process.env.REDIS_HOST || "127.0.0.1",
  port: 6379,
  maxRetriesPerRequest: null,
};

const redis = new Redis(redisOptions);
redis.on("connect", () => {
  console.log("✅ Redis successfully connected");
});
redis.on("error", (err) => {
  console.error("❌ Redis connection error:", err);
});
module.exports = redis;
