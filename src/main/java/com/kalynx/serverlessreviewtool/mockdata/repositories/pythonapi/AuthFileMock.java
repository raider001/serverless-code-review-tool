package com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class AuthFileMock extends BaseRepository {
    private static final String FILE_PATH = "app/routes/auth.py";
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
        c.append("from flask import Blueprint, request, jsonify\n");
        if (i >= 2) c.append("from app.models.models import User, db\n");
        if (i >= 4) c.append("import jwt\nfrom datetime import datetime, timedelta\nfrom functools import wraps\n");
        if (i >= 9) c.append("import logging\n\nlogger = logging.getLogger(__name__)\n");
        c.append("\nauth_bp = Blueprint('auth', __name__)\n");
        if (i >= 4) c.append("SECRET_KEY = 'your-secret-key-here'\n");
        c.append("\n");
        if (i >= 5) {
            c.append("def token_required(f):\n    @wraps(f)\n    def decorated(*args, **kwargs):\n");
            c.append("        token = request.headers.get('Authorization')\n");
            c.append("        if not token:\n            return jsonify({'error': 'Token missing'}), 401\n");
            c.append("        try:\n");
            if (i >= 6) c.append("            token = token.split(' ')[1]\n");
            c.append("            data = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])\n");
            if (i >= 10) c.append("            current_user = User.query.get(data['user_id'])\n            if not current_user:\n                return jsonify({'error': 'User not found'}), 401\n");
            c.append("        except Exception as e:\n");
            if (i >= 9) c.append("            logger.error(f'Token error: {str(e)}')\n");
            c.append("            return jsonify({'error': 'Invalid token'}), 401\n");
            c.append("        return f(").append(i >= 10 ? "current_user, " : "").append("*args, **kwargs)\n    return decorated\n\n");
        }
        c.append("@auth_bp.route('/register', methods=['POST'])\ndef register():\n");
        if (i >= 1) {
            c.append("    data = request.get_json()\n");
            if (i >= 7) c.append("    if not data or not data.get('username') or not data.get('password'):\n        return jsonify({'error': 'Missing fields'}), 400\n\n");
        }
        if (i >= 9) c.append("    logger.info(f'Registration for: {data.get(\"username\")}')\n");
        if (i >= 2) {
            c.append("    user = User(username=data['username'], email=data['email'])\n");
            if (i >= 3) c.append("    user.set_password(data['password'])\n");
            c.append("    db.session.add(user)\n");
            if (i >= 8) {
                c.append("    try:\n        db.session.commit()\n");
                if (i >= 9) c.append("        logger.info(f'User registered: {user.username}')\n");
                c.append("    except Exception as e:\n");
                if (i >= 9) c.append("        logger.error(f'Registration failed: {str(e)}')\n");
                c.append("        db.session.rollback()\n        return jsonify({'error': 'User exists'}), 400\n");
            } else {
                c.append("    db.session.commit()\n");
            }
            c.append("    return jsonify({'message': 'User registered'}), 201\n");
        } else {
            c.append("    return jsonify({'message': 'Register endpoint'})\n");
        }
        c.append("\n");
        if (i >= 4) {
            c.append("@auth_bp.route('/login', methods=['POST'])\ndef login():\n    data = request.get_json()\n");
            if (i >= 7) c.append("    if not data or not data.get('username'):\n        return jsonify({'error': 'Missing credentials'}), 400\n\n");
            if (i >= 9) c.append("    logger.info(f'Login attempt: {data.get(\"username\")}')\n");
            c.append("    user = User.query.filter_by(username=data['username']).first()\n");
            c.append("    if user and user.check_password(data['password']):\n");
            if (i >= 11) c.append("        user.last_login = datetime.utcnow()\n        db.session.commit()\n");
            c.append("        token = jwt.encode({'user_id': user.id, 'exp': datetime.utcnow() + timedelta(hours=24)}, SECRET_KEY, algorithm='HS256')\n");
            if (i >= 9) c.append("        logger.info(f'Login successful: {user.username}')\n");
            c.append("        return jsonify({'token': token})\n");
            if (i >= 9) c.append("    logger.warning(f'Failed login: {data.get(\"username\")}')\n");
            c.append("    return jsonify({'error': 'Invalid credentials'}), 401\n\n");
        }
        if (i >= 12) {
            c.append("@auth_bp.route('/refresh', methods=['POST'])\n@token_required\ndef refresh(current_user):\n");
            c.append("    token = jwt.encode({'user_id': current_user.id, 'exp': datetime.utcnow() + timedelta(hours=24)}, SECRET_KEY, algorithm='HS256')\n");
            c.append("    logger.info(f'Token refreshed: {current_user.username}')\n    return jsonify({'token': token})\n\n");
        }
        if (i >= 14) {
            c.append("@auth_bp.route('/logout', methods=['POST'])\n@token_required\ndef logout(current_user):\n");
            c.append("    logger.info(f'Logout: {current_user.username}')\n    return jsonify({'message': 'Logged out'})\n");
        }
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create auth blueprint";
            case 1 -> "feat: Add JSON parsing to register";
            case 2 -> "feat: Implement user creation";
            case 3 -> "feat: Add password hashing";
            case 4 -> "feat: Add JWT and login endpoint";
            case 5 -> "feat: Add token_required decorator";
            case 6 -> "fix: Parse Bearer token";
            case 7 -> "feat: Add input validation";
            case 8 -> "feat: Add duplicate user handling";
            case 9 -> "feat: Add logging";
            case 10 -> "refactor: Inject current_user";
            case 11 -> "feat: Track last_login";
            case 12 -> "feat: Add token refresh";
            case 14 -> "feat: Add logout endpoint";
            default -> "refactor: Auth improvements " + (i + 1);
        };
    }
}

