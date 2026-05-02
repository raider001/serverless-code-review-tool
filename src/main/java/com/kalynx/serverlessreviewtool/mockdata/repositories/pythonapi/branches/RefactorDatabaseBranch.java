package com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class RefactorDatabaseBranch extends BaseRepository {

    public static void create(Path repoPath) throws Exception {
        createAndCheckoutBranch(repoPath, "refactor/async-database");

        Path dbFile = repoPath.resolve("src/database_async.py");
        Files.writeString(dbFile, """
            import asyncio
            import asyncpg

            class AsyncDatabase:
                async def connect(self, connection_string):
                    self.pool = await asyncpg.create_pool(connection_string)

                async def execute(self, query, *params):
                    async with self.pool.acquire() as conn:
                        return await conn.execute(query, *params)
            """);
        commitFile(repoPath, "src/database_async.py", "refactor: Add async database connection pool");

        Files.writeString(dbFile, """
            import asyncio
            import asyncpg
            from contextlib import asynccontextmanager

            class AsyncDatabase:
                def __init__(self):
                    self.pool = None

                async def connect(self, connection_string, min_size=10, max_size=20):
                    self.pool = await asyncpg.create_pool(
                        connection_string,
                        min_size=min_size,
                        max_size=max_size
                    )

                @asynccontextmanager
                async def transaction(self):
                    async with self.pool.acquire() as conn:
                        async with conn.transaction():
                            yield conn

                async def execute(self, query, *params):
                    async with self.pool.acquire() as conn:
                        return await conn.execute(query, *params)
            """);
        commitFile(repoPath, "src/database_async.py", "refactor: Add transaction support and pool configuration");

        checkoutMain(repoPath);
    }
}

