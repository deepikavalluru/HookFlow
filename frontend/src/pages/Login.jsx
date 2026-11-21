import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Zap, Mail, Lock, ArrowRight } from "lucide-react";
import { useAuth } from "../context/AuthContext.jsx";

function Login() {

    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        email: "",
        password: ""
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        setError("");
        setLoading(true);

        try {

            await login(formData);

            navigate("/dashboard", { replace: true });

        } catch (err) {

            setError(
                err.response?.data?.message ||
                "Invalid email or password."
            );

        } finally {

            setLoading(false);

        }
    };

    return (

        <div className="min-h-screen bg-slate-950 flex items-center justify-center px-4">

            <div className="w-full max-w-md">

                {/* Logo */}

                <div className="flex items-center justify-center gap-3 mb-8">

                    <div className="bg-indigo-600 p-3 rounded-xl">
                        <Zap size={28} className="text-white" />
                    </div>

                    <h1 className="text-3xl font-bold text-white">
                        HookFlow
                    </h1>

                </div>

                {/* Login Card */}

                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl">

                    <h2 className="text-2xl font-bold text-white mb-2">
                        Welcome back
                    </h2>

                    <p className="text-slate-400 mb-8">
                        Sign in to manage your webhook infrastructure.
                    </p>

                    {location.state?.message && (
                        <div className="bg-green-500/10 border border-green-500/30 text-green-400 p-3 rounded-lg mb-5 text-sm">
                            {location.state.message}
                        </div>
                    )}

                    {error && (

                        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-3 rounded-lg mb-5 text-sm">
                            {error}
                        </div>

                    )}

                    <form onSubmit={handleSubmit}>

                        {/* Email */}

                        <div className="mb-5">

                            <label className="block text-sm text-slate-300 mb-2">
                                Email address
                            </label>

                            <div className="relative">

                                <Mail
                                    size={18}
                                    className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
                                />

                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    placeholder="you@example.com"
                                    required
                                    className="w-full bg-slate-950 border border-slate-700 rounded-lg py-3 pl-11 pr-4 text-white outline-none focus:border-indigo-500 transition"
                                />

                            </div>

                        </div>

                        {/* Password */}

                        <div className="mb-7">

                            <label className="block text-sm text-slate-300 mb-2">
                                Password
                            </label>

                            <div className="relative">

                                <Lock
                                    size={18}
                                    className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
                                />

                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="Enter your password"
                                    required
                                    className="w-full bg-slate-950 border border-slate-700 rounded-lg py-3 pl-11 pr-4 text-white outline-none focus:border-indigo-500 transition"
                                />

                            </div>

                        </div>

                        {/* Submit */}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold py-3 rounded-lg flex items-center justify-center gap-2 transition"
                        >

                            {loading ? "Signing in..." : "Sign in"}

                            {!loading && <ArrowRight size={18} />}

                        </button>

                    </form>

                    <div className="mt-7 text-center text-sm text-slate-400">

                        Don't have an account?{" "}

                        <Link
                            to="/register"
                            className="text-indigo-400 hover:text-indigo-300 font-medium"
                        >
                            Create account
                        </Link>

                    </div>

                </div>

                <p className="text-center text-slate-600 text-xs mt-8">
                    HookFlow · Reliable Webhook Delivery
                </p>

            </div>

        </div>

    );
}

export default Login;