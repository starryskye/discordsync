package com.conaxgames.discordsync.bot.util;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoleUtil {
    public static boolean hasRole(Member member, String... names) {
        for (Role role : member.getRoles()) {
            for (String name : names) {
                if (role.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static List<Role> getRemoveList(Member member, List<Role> rankRoles) {
        List<Role> remove = new ArrayList<>();

        Iterator<Role> it = member.getRoles().iterator();
        removeLoop: while (it.hasNext()) {
            Role role = it.next();
            String roleName = role.getName();
            if (roleName.equalsIgnoreCase("UHC")
                    || roleName.contains("Permissions")
                    || roleName.contains("Verified")
                    || roleName.contains("Player")) {
                continue;
            }

            if (rankRoles != null) {
                for (Role rankRole : rankRoles) {
                    if (role.equals(rankRole)) {
                        continue removeLoop;
                    }
                }
            }

            remove.add(role);
        }

        return remove;
    }
}
