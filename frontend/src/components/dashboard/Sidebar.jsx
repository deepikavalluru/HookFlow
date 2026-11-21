import { useState } from "react";

import {
    Zap,
    LayoutDashboard,
    Webhook,
    Activity,
    Send,
    Settings,
    LogOut,
    Menu,
    X
} from "lucide-react";

import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";

function Sidebar() {

    const { logout } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const navigate = useNavigate();

    const navigation = [
        { name: "Overview", path: "/dashboard", icon: LayoutDashboard },
        { name: "Webhooks", path: "/webhooks", icon: Webhook },
        { name: "Events", path: "/events", icon: Activity },
        { name: "Deliveries", path: "/deliveries", icon: Send },
        { name: "Settings", path: "/settings", icon: Settings }
    ];

    const handleLogout = () => {
        setIsOpen(false);
        logout();
    };

    return (
        <>
            {/* Mobile Menu Button */}

            <button
                onClick={() => setIsOpen(true)}
                className="fixed top-5 left-4 z-50 p-2 rounded-lg bg-slate-800 text-white md:hidden"
                aria-label="Open navigation menu"
            >
                <Menu size={23} />
            </button>

            {/* Mobile Overlay */}

            {isOpen && (
                <div
                    className="fixed inset-0 bg-black/60 z-40 md:hidden"
                    onClick={() => setIsOpen(false)}
                />
            )}

            {/* Sidebar */}

            <aside
                className={`
                    fixed md:sticky top-0 left-0 z-50
                    w-64 h-screen bg-slate-900
                    border-r border-slate-800
                    flex flex-col shrink-0
                    transform transition-transform duration-300
                    ${isOpen ? "translate-x-0" : "-translate-x-full md:translate-x-0"}
                `}
            >

                {/* Logo */}

                <div className="h-20 flex items-center justify-between gap-3 px-6 border-b border-slate-800">

                    <div className="flex items-center gap-3">

                        <div className="bg-indigo-600 p-2 rounded-lg">
                            <Zap size={22} className="text-white" />
                        </div>

                        <span className="text-xl font-bold text-white">
                            HookFlow
                        </span>

                    </div>

                    <button
                        onClick={() => setIsOpen(false)}
                        className="text-slate-400 hover:text-white md:hidden"
                        aria-label="Close navigation menu"
                    >
                        <X size={22} />
                    </button>

                </div>

                {/* Navigation */}

                <nav className="flex-1 p-4 space-y-2">

                    <p className="text-xs uppercase tracking-wider text-slate-500 px-3 mb-4">
                        Workspace
                    </p>

                    {navigation.map((item) => {

                        const Icon = item.icon;

                        return (
                            <NavLink
                                key={item.name}
                                to={item.path}
                                end={item.path === "/dashboard"}
                                onClick={() => setIsOpen(false)}
                                className={({ isActive }) =>
                                    `flex items-center gap-3 px-3 py-3 rounded-lg text-sm transition ${
                                        isActive
                                            ? "bg-indigo-600/15 text-indigo-400"
                                            : "text-slate-400 hover:bg-slate-800 hover:text-white"
                                    }`
                                }
                            >

                                <Icon size={19} />

                                {item.name}

                            </NavLink>
                        );
                    })}

                </nav>

                {/* Logout */}

                <div className="p-4 border-t border-slate-800">

                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 w-full px-3 py-3 rounded-lg text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition text-sm"
                    >

                        <LogOut size={19} />

                        Logout

                    </button>

                </div>

            </aside>
        </>
    );
}

export default Sidebar;