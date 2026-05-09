package com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class DatabaseFileMock extends BaseRepository {
    private static final String FILE_PATH = "app/database.py";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(16);
        for (int i = 0; i < commitCount; i++) {
            Files.writeString(repoPath.resolve(FILE_PATH), generate(i));
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generate(int i) {
        var c = new StringBuilder();
        c.append("from app.app import db\n");
        if (i >= 3) c.append("from sqlalchemy import create_engine\nfrom sqlalchemy.orm import scoped_session, sessionmaker\n");
        if (i >= 6) c.append("import logging\n\nlogger = logging.getLogger(__name__)\n");
        c.append("\ndef init_db():\n");
        if (i >= 1) {
            c.append("    db.create_all()\n");
            if (i >= 6) c.append("    logger.info('Database initialized')\n");
        } else {
            c.append("    pass\n");
        }
        c.append("\n");
        if (i >= 2) {
            c.append("def drop_db():\n    db.drop_all()\n");
            if (i >= 6) c.append("    logger.info('Database dropped')\n");
            c.append("\n");
        }
        if (i >= 4) {
            c.append("def reset_db():\n    drop_db()\n    init_db()\n");
            if (i >= 6) c.append("    logger.info('Database reset')\n");
            c.append("\n");
        }
        if (i >= 5) {
            c.append("def seed_db():\n    from app.models.models import User\n    \n");
            if (i >= 7) c.append("    if User.query.first():\n").append(i >= 6 ? "        logger.info('Already seeded')\n" : "").append("        return\n    \n");
            c.append("    admin = User(username='admin', email='admin@example.com')\n    admin.set_password('admin123')\n");
            if (i >= 8) c.append("    admin.first_name = 'Admin'\n    admin.last_name = 'User'\n");
            c.append("    db.session.add(admin)\n");
            if (i >= 9) {
                c.append("    \n    test_user = User(username='testuser', email='test@example.com')\n");
                c.append("    test_user.set_password('test123')\n");
                c.append("    test_user.first_name = 'Test'\n    test_user.last_name = 'User'\n");
                c.append("    db.session.add(test_user)\n");
            }
            c.append("    db.session.commit()\n");
            if (i >= 6) c.append("    logger.info('Database seeded')\n");
            c.append("\n");
        }
        if (i >= 10) {
            c.append("def get_db_stats():\n    from app.models.models import User\n    \n");
            c.append("    stats = {'total_users': User.query.count(), 'active_users': User.query.filter_by(is_active=True).count()}\n");
            if (i >= 11) {
                c.append("    from app.models.models import Session\n");
                c.append("    stats['active_sessions'] = Session.query.count()\n");
            }
            if (i >= 6) c.append("    logger.debug(f'Stats: {stats}')\n");
            c.append("    return stats\n\n");
        }
        if (i >= 12) {
            c.append("def backup_db(backup_path):\n    import shutil, os\n    \n");
            c.append("    db_path = 'app/users.db'\n    if os.path.exists(db_path):\n");
            c.append("        shutil.copy2(db_path, backup_path)\n        logger.info(f'Backed up to {backup_path}')\n");
            c.append("        return True\n    logger.warning('Database not found')\n    return False\n\n");
        }
        if (i >= 14) {
            c.append("def check_db_health():\n    try:\n        db.session.execute('SELECT 1')\n");
            c.append("        logger.info('Health check passed')\n        return True\n");
            c.append("    except Exception as e:\n        logger.error(f'Health check failed: {str(e)}')\n");
            c.append("        return False\n");
        }
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create database module";
            case 1 -> "feat: Implement init_db";
            case 2 -> "feat: Add drop_db";
            case 3 -> "refactor: Add SQLAlchemy imports";
            case 4 -> "feat: Add reset_db";
            case 5 -> "feat: Add seed_db with admin";
            case 6 -> "feat: Add logging";
            case 7 -> "fix: Check if already seeded";
            case 8 -> "refactor: Add admin names";
            case 9 -> "feat: Add test user to seed";
            case 10 -> "feat: Add get_db_stats";
            case 11 -> "refactor: Include session stats";
            case 12 -> "feat: Add database backup";
            case 14 -> "feat: Add health check";
            default -> "refactor: Database improvements " + (i + 1);
        };
    }
}

