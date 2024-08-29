import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const RESTAURANT_ID = "e3b0c442-98fc-1fc1-9fd3-256e9df06d05";
const ORDER_ID = "e3b0c442-98fc-1fc1-9fd3-256e9df06d68";

export let options = {
    stages: [
        { duration: '30s', target: 200 },  // ramp up to X users
        { duration: '1m', target: 200 },   // stay at X users for 1 minute
        { duration: '30s', target: 0 },   // ramp down to 0 users
    ],
    thresholds: {
        'http_req_duration': ['p(95)<100'],  // 95% of requests must complete below 200ms
    },
};

const BASE_URL = 'http://localhost:8080/api/reviews';

let getReviewDuration = new Trend('get_review_duration');

export default function () {
    // Get Review by Order ID
    let getRes = http.get(`${BASE_URL}/order/${ORDER_ID}`);
    getReviewDuration.add(getRes.timings.duration);
    check(getRes, { 'retrieved review successfully': (r) => r.status === 200 });

    sleep(1);
}

export function handleSummary(data) {
    const summaryFileName = `getReview-summary-${RESTAURANT_ID}.html`;
    return {
        [summaryFileName]: htmlReport(data),
    };
}
