package com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi.branches;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class RefactorDatabaseBranch extends BaseRepository {

    public static void create(Path repoPath) throws Exception {
        createAndCheckoutBranch(repoPath, "refactor/async-database");

        Path dbFile = repoPath.resolve("src/database_async.py");
        Files.writeString(dbFile,
            "import asyncio\n" +
            "import asyncpg\n" +
            "\n" +
            "class AsyncDatabase:\n" +
            "    async def connect(self, connection_string):\n" +
            "        self.pool = await asyncpg.create_pool(connection_string)\n" +
            "    \n" +
            "    async def execute(self, query, *params):\n" +
            "        async with self.pool.acquire() as conn:\n" +
            "            return await conn.execute(query, *params)\n");
        commitFile(repoPath, "src/database_async.py", "refactor: Add async database connection pool");

        Files.writeString(dbFile,
            "import asyncio\n" +
            "import asyncpg\n" +
            "from contextlib import asynccontextmanager\n" +
            "\n" +
            "class AsyncDatabase:\n" +
            "    def __init__(self):\n" +
            "        self.pool = None\n" +
            "    \n" +
            "    async def connect(self, connection_string, min_size=10, max_size=20):\n" +
            "        self.pool = await asyncpg.create_pool(\n" +
            "            connection_string,\n" +
            "            min_size=min_size,\n" +
            "            max_size=max_size\n" +
            "        )\n" +
            "    \n" +
            "    @asynccontextmanager\n" +
            "    async def transaction(self):\n" +
            "        async with self.pool.acquire() as conn:\n" +
            "            async with conn.transaction():\n" +
            "                yield conn\n" +
            "    \n" +
            "    async def execute(self, query, *params):\n" +
            "        async with self.pool.acquire() as conn:\n" +
            "            return await conn.execute(query, *params)\n");
        commitFile(repoPath, "src/database_async.py", "refactor: Add transaction support and pool configuration");

        checkoutMain(repoPath);
    }
}

