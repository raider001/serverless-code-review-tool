package com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class AppFileMock extends BaseRepository {
    private static final String FILE_PATH = "app/app.py";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(26);

        for (int i = 0; i < commitCount; i++) {
            String content = generateContentForIteration(i);
            Path filePath = repoPath.resolve(FILE_PATH);
            Files.writeString(filePath, content);
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generateContentForIteration(int iteration) {
        StringBuilder content = new StringBuilder();

        if (iteration >= 1) {
            content.append("from flask import Flask, jsonify, request\n");
        } else {
            content.append("from flask import Flask\n");
        }
        if (iteration >= 3) {
            content.append("from flask_sqlalchemy import SQLAlchemy\n");
            if (iteration < 18) {
                content.append("from flask_cors import CORS\n");
            }
        }
        if (iteration >= 6 && iteration < 22) {
            content.append("from app.routes import auth_bp, user_bp\n");
        }
        if (iteration >= 22) {
            content.append("from app.routes.auth import auth_bp\n");
            content.append("from app.routes.users import user_bp\n");
        }
        if (iteration >= 9) {
            content.append("import logging\n");
            if (iteration < 25) {
                content.append("from logging.handlers import RotatingFileHandler\n");
            }
        }
        if (iteration >= 18) {
            content.append("from flask_cors import CORS\n");
            content.append("from app.config import Config\n");
        }
        if (iteration >= 28) {
            content.append("from app.middleware import request_logger, error_handler\n");
        }
        content.append("\n");

        if (iteration < 18) {
            content.append("app = Flask(__name__)\n");
        } else {
            content.append("def create_app(config=None):\n");
            content.append("    app = Flask(__name__)\n");
        }

        if (iteration >= 2 && iteration < 18) {
            content.append("app.config['SECRET_KEY'] = 'dev-secret-key'\n");
        } else if (iteration >= 18) {
            content.append("    app.config.from_object(Config if config is None else config)\n");
        }

        if (iteration >= 3 && iteration < 18) {
            content.append("app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///users.db'\n");
            content.append("app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False\n");
            content.append("\ndb = SQLAlchemy(app)\n");
            if (iteration < 18) {
                content.append("CORS(app)\n");
            }
        } else if (iteration >= 18) {
            content.append("    \n");
            content.append("    db = SQLAlchemy(app)\n");
            content.append("    CORS(app, resources={r\"/api/*\": {\"origins\": app.config['CORS_ORIGINS']}})\n");
        }

        if (iteration >= 18) {
            content.append("    \n");
        }

        if (iteration >= 9 && iteration < 25) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("if not app.debug:\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    handler = RotatingFileHandler('app.log', maxBytes=10000, backupCount=3)\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    handler.setLevel(logging.INFO)\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    app.logger.addHandler(handler)\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        } else if (iteration >= 25) {
            content.append("    logging.basicConfig(level=logging.INFO if not app.debug else logging.DEBUG)\n");
            content.append("    \n");
        }

        if (iteration >= 6) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("app.register_blueprint(auth_bp, url_prefix='/api/auth')\n");
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("app.register_blueprint(user_bp, url_prefix='/api/users')\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        }

        if (iteration >= 28) {
            content.append("    app.before_request(request_logger)\n");
            content.append("    app.register_error_handler(Exception, error_handler)\n");
            content.append("    \n");
        }

        if (iteration >= 18) {
            content.append("    ");
        }
        content.append("@app.route('/')\n");
        if (iteration >= 18) {
            content.append("    ");
        }
        content.append("def index():\n");
        if (iteration >= 1) {
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    return jsonify({'message': 'Welcome to User API'");
            if (iteration >= 20) {
                content.append(", 'version': '2.0'");
            }
            content.append("})\n");
        } else {
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    return 'Welcome to User API'\n");
        }

        if (iteration >= 18) {
            content.append("    \n");
        } else {
            content.append("\n");
        }

        if (iteration >= 4) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("@app.route('/health')\n");
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("def health():\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    return jsonify({'status': 'healthy'");
            if (iteration >= 26) {
                content.append(", 'database': 'connected'");
            }
            content.append("})\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        }

        if (iteration >= 7) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("@app.errorhandler(404)\n");
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("def not_found(error):\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    return jsonify({'error': 'Not found'}), 404\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        }

        if (iteration >= 8) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("@app.errorhandler(500)\n");
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("def internal_error(error):\n");
            if (iteration >= 9) {
                if (iteration >= 18) {
                    content.append("        ");
                }
                content.append("    app.logger.error(f'Internal error: {error}')\n");
            }
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    return jsonify({'error': 'Internal server error'}), 500\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        }

        if (iteration >= 10 && iteration < 28) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("@app.before_request\n");
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("def log_request():\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    app.logger.info(f'{request.method} {request.path}')\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        }

        if (iteration >= 12 && iteration < 28) {
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("@app.after_request\n");
            if (iteration >= 18) {
                content.append("    ");
            }
            content.append("def after_request(response):\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    response.headers['X-Content-Type-Options'] = 'nosniff'\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    response.headers['X-Frame-Options'] = 'DENY'\n");
            if (iteration >= 18) {
                content.append("        ");
            }
            content.append("    return response\n");
            if (iteration >= 18) {
                content.append("    \n");
            } else {
                content.append("\n");
            }
        }

        if (iteration >= 18) {
            content.append("    return app\n\n");
        }

        content.append("if __name__ == '__main__':\n");
        if (iteration >= 18) {
            content.append("    app = create_app()\n");
            content.append("    ");
        }
        if (iteration >= 5) {
            content.append("app.run(debug=True, host='0.0.0.0', port=5000)\n");
        } else {
            content.append("app.run(debug=True)\n");
        }

        return content.toString();
    }

    private static String getCommitMessage(int iteration) {
        return switch (iteration) {
            case 0 -> "feat: Create initial Flask application";
            case 1 -> "feat: Add JSON response support";
            case 2 -> "feat: Add secret key configuration";
            case 3 -> "feat: Add SQLAlchemy and CORS support";
            case 4 -> "feat: Add health check endpoint";
            case 5 -> "feat: Configure host and port for production";
            case 6 -> "feat: Register auth and user blueprints";
            case 7 -> "feat: Add 404 error handler";
            case 8 -> "feat: Add 500 error handler";
            case 9 -> "feat: Add logging configuration";
            case 10 -> "feat: Add request logging middleware";
            case 11 -> "refactor: Improve error handling";
            case 12 -> "feat: Add security headers";
            case 13 -> "refactor: Update CORS configuration";
            case 14 -> "refactor: Organize imports";
            case 15 -> "feat: Add application monitoring";
            case 16 -> "refactor: Extract configuration";
            case 17 -> "test: Add tests for endpoints";
            case 18 -> "refactor: Convert to application factory pattern";
            case 19 -> "refactor: Use Config class for settings";
            case 20 -> "feat: Add version to API response";
            case 21 -> "refactor: Improve blueprint registration";
            case 22 -> "refactor: Split routes imports for clarity";
            case 23 -> "refactor: Update CORS to use config";
            case 24 -> "refactor: Improve logging configuration";
            case 25 -> "refactor: Simplify logging setup";
            case 26 -> "feat: Add database status to health check";
            case 27 -> "refactor: Remove redundant logging middleware";
            case 28 -> "refactor: Extract middleware to separate module";
            case 29 -> "refactor: Improve error handler registration";
            case 30 -> "perf: Optimize application startup";
            case 31 -> "refactor: Add type hints";
            case 32 -> "docs: Add docstrings";
            case 33 -> "refactor: Final code cleanup";
            case 34 -> "test: Add integration tests";
            case 35 -> "refactor: Improve configuration management";
            case 36 -> "feat: Add graceful shutdown handler";
            case 37 -> "refactor: Code review improvements";
            case 38 -> "docs: Update API documentation";
            case 39 -> "refactor: Optimize imports";
            case 40 -> "chore: Final polish";
            default -> "refactor: Application improvements iteration " + (iteration + 1);
        };
    }
}

