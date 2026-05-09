package com.kalynx.serverlessreviewtool.mockdata.repositories.reactfrontend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class UserListFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/components/UserList.tsx";
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
        c.append("import React, { useState, useEffect } from 'react';\n");
        if (i >= 2) c.append("import { api } from '../services/api';\n");
        c.append("\ninterface User { id: number; username: string; email: string");
        if (i >= 7) c.append("; first_name?: string; last_name?: string");
        if (i >= 10) c.append("; is_active?: boolean");
        c.append("; }\n");
        if (i >= 1) c.append("\ninterface UserListProps { token: string | null; }\n\n");
        c.append("export const UserList");
        if (i >= 1) c.append(": React.FC<UserListProps> = ({ token })");
        else c.append(" = ()");
        c.append(" => {\n");
        c.append("  const [users, setUsers] = useState<User[]>([]);\n");
        if (i >= 3) c.append("  const [loading, setLoading] = useState(true);\n");
        if (i >= 4) c.append("  const [error, setError] = useState<string | null>(null);\n");
        if (i >= 8) c.append("  const [searchTerm, setSearchTerm] = useState('');\n");
        c.append("\n  useEffect(() => {\n");
        if (i >= 2) c.append("    fetchUsers();\n");
        else c.append("    setUsers([]);\n");
        c.append("  }, [").append(i >= 11 ? "token" : "").append("]);\n\n");
        if (i >= 2) {
            c.append("  const fetchUsers = async () => {\n");
            if (i >= 3) c.append("    setLoading(true);\n");
            if (i >= 4) c.append("    setError(null);\n");
            c.append("    try {\n      const data = await api.getUsers(token || '');\n      setUsers(data);\n");
            c.append("    } catch (err) {\n");
            if (i >= 4) c.append("      setError(err instanceof Error ? err.message : 'Failed to fetch users');\n");
            if (i >= 6) c.append("      console.error('Error fetching users:', err);\n");
            c.append("    }");
            if (i >= 3) c.append(" finally {\n      setLoading(false);\n    }");
            c.append("\n  };\n\n");
        }
        if (i >= 9) c.append("  const handleRefresh = () => { fetchUsers(); };\n\n");
        if (i >= 8) {
            c.append("  const filteredUsers = users.filter(user =>\n");
            c.append("    user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||\n");
            c.append("    user.email.toLowerCase().includes(searchTerm.toLowerCase())\n  );\n\n");
        }
        if (i >= 3) c.append("  if (loading) return <div>Loading users...</div>;\n");
        if (i >= 4) c.append("  if (error) return <div className=\"error\">{error}</div>;\n\n");
        c.append("  return (\n    <div className=\"user-list\">\n");
        if (i >= 5) c.append("      <h2>Users</h2>\n");
        if (i >= 9) c.append("      <button onClick={handleRefresh}>Refresh</button>\n");
        if (i >= 8) c.append("      <input type=\"text\" placeholder=\"Search users...\" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />\n");
        if (i >= 12) {
            c.append("      <p>Total users: {").append(i >= 8 ? "filteredUsers" : "users").append(".length}</p>\n");
        }
        c.append("      <ul>\n        {");
        c.append(i >= 8 ? "filteredUsers" : "users");
        c.append(".map(user => (\n          <li key={user.id}>\n");
        if (i >= 7) {
            c.append("            <strong>{user.first_name && user.last_name ? `${user.first_name} ${user.last_name}` : user.username}</strong>\n");
        } else {
            c.append("            <strong>{user.username}</strong>\n");
        }
        c.append("            ").append(i >= 6 ? "<span>({user.email})</span>" : "- {user.email}").append("\n");
        if (i >= 10) c.append("            {!user.is_active && <span className=\"inactive\"> [Inactive]</span>}\n");
        c.append("          </li>\n        ))}\n      </ul>\n");
        if (i >= 13) {
            c.append("      {").append(i >= 8 ? "filteredUsers" : "users").append(".length === 0 && <p>No users found</p>}\n");
        }
        c.append("    </div>\n  );\n};\n");
        return c.toString();
    }

    private static String getCommitMessage(int i) {
        return switch (i) {
            case 0 -> "feat: Create UserList component";
            case 1 -> "feat: Add token prop";
            case 2 -> "feat: Fetch users from API";
            case 3 -> "feat: Add loading state";
            case 4 -> "feat: Add error handling";
            case 5 -> "feat: Add component title";
            case 6 -> "refactor: Improve display format";
            case 7 -> "feat: Display full name";
            case 8 -> "feat: Add search functionality";
            case 9 -> "feat: Add refresh button";
            case 10 -> "feat: Show inactive indicator";
            case 11 -> "fix: Refetch users when token changes";
            case 12 -> "feat: Display user count";
            case 13 -> "feat: Show empty state";
            default -> "refactor: UserList improvements " + (i + 1);
        };
    }
}

