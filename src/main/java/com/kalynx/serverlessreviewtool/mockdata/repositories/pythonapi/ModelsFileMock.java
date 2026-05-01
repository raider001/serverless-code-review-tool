package com.kalynx.serverlessreviewtool.mockdata.repositories.pythonapi;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class ModelsFileMock extends BaseRepository {
    private static final String FILE_PATH = "app/models/models.py";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(26);
        for (int i = 0; i < commitCount; i++) {
            Files.writeString(repoPath.resolve(FILE_PATH), generate(i));
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generate(int i) {
        var c = new StringBuilder();
        c.append("from app.app import db\n");
        if (i >= 3) c.append("from datetime import datetime\n");
        if (i >= 6) c.append("from werkzeug.security import generate_password_hash, check_password_hash\n");
        if (i >= 20) c.append("from typing import Optional, Dict, Any\n");
        c.append("\nclass User(db.Model):\n");
        if (i >= 2) c.append("    __tablename__ = 'users'\n\n");
        c.append("    id = db.Column(db.Integer, primary_key=True)\n    username = db.Column(db.String(80), unique=True, nullable=False)\n    email = db.Column(db.String(120), unique=True, nullable=False)\n");
        if (i >= 1) c.append("    password_hash = db.Column(db.String(255), nullable=False)\n");
        if (i >= 3) c.append("    created_at = db.Column(db.DateTime, default=datetime.utcnow)\n");
        if (i >= 4) c.append("    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)\n");
        if (i >= 5) c.append("    is_active = db.Column(db.Boolean, default=True)\n");
        if (i >= 8) c.append("    first_name = db.Column(db.String(50))\n    last_name = db.Column(db.String(50))\n");
        if (i >= 11) c.append("    last_login = db.Column(db.DateTime)\n");
        c.append("\n");
        if (i >= 6) {
            c.append("    def set_password(self, password").append(i >= 20 ? ": str" : "").append(")").append(i >= 20 ? " -> None" : "").append(":\n        self.password_hash = generate_password_hash(password)\n\n");
            c.append("    def check_password(self, password").append(i >= 20 ? ": str" : "").append(")").append(i >= 20 ? " -> bool" : "").append(":\n        return check_password_hash(self.password_hash, password)\n\n");
        }
        if (i >= 7) {
            c.append("    def to_dict(self)").append(i >= 20 ? " -> Dict[str, Any]" : "").append(":\n        return {'id': self.id, 'username': self.username, 'email': self.email");
            if (i >= 8) c.append(", 'first_name': self.first_name, 'last_name': self.last_name");
            if (i >= 9) c.append(", 'is_active': self.is_active");
            c.append("}\n\n");
        }
        if (i >= 17 && i < 22) c.append("    @staticmethod\n    def find_by_email(email):\n        return User.query.filter_by(email=email).first()\n\n");
        c.append("    def __repr__(self)").append(i >= 20 ? " -> str" : "").append(":\n        return f'<User {self.username}>'\n");
        if (i >= 13) {
            c.append("\n\nclass Session(db.Model):\n    __tablename__ = 'sessions'\n    id = db.Column(db.Integer, primary_key=True)\n");
            c.append("    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)\n");
            c.append("    token = db.Column(db.String(255), unique=True, nullable=False)\n");
            c.append("    created_at = db.Column(db.DateTime, default=datetime.utcnow)\n");
            c.append("    expires_at = db.Column(db.DateTime, nullable=False)\n");
            c.append("    user = db.relationship('User', backref=db.backref('sessions', lazy=True))\n\n");
            c.append("    def __repr__(self):\n        return f'<Session {self.id}>'\n");
        }
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create initial User model";
            case 1 -> "feat: Add password hash field";
            case 2 -> "refactor: Add table name specification";
            case 3 -> "feat: Add created_at timestamp";
            case 4 -> "feat: Add updated_at timestamp";
            case 5 -> "feat: Add is_active flag";
            case 6 -> "feat: Add password has hing methods";
            case 7 -> "feat: Add to_dict serialization";
            case 8 -> "feat: Add name fields";
            case 11 -> "feat: Add last_login tracking";
            case 13 -> "feat: Add Session model";
            case 17 -> "feat: Add find_by_email static method";
            case 20 -> "refactor: Add type hints";
            case 22 -> "refactor: Remove find_by_email";
            default -> "refactor: Model improvements " + (i + 1);
        };
    }
}

