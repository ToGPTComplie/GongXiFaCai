import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppShell } from "./App";
import { UserDashboardPage } from "./pages/UserDashboardPage";
import { UserTransactionsPage } from "./pages/UserTransactionsPage";
import { UserWatchlistPage } from "./pages/UserWatchlistPage";
import { WatchlistDetailPage } from "./pages/WatchlistDetailPage";
import { UserTargetAllocationsPage } from "./pages/UserTargetAllocationsPage";
import { UserPerformancePage } from "./pages/UserPerformancePage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Navigate to="/users/1" replace />,
  },
  {
    path: "/users/:id",
    element: <AppShell />,
    children: [
      {
        index: true,
        element: <UserDashboardPage />,
      },
      {
        path: "transactions",
        element: <UserTransactionsPage />,
      },
      {
        path: "watchlist",
        element: <UserWatchlistPage />,
      },
      {
        path: "watchlist/:ticker",
        element: <WatchlistDetailPage />,
      },
      {
        path: "targets",
        element: <UserTargetAllocationsPage />,
      },
      {
        path: "performance",
        element: <UserPerformancePage />,
      },
    ],
  },
]);
