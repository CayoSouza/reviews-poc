import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

// Utility function to generate a UUID
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0,
            v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

const RESTAURANT_ID = "e3b0c442-98fc-1fc1-9fd3-256e9df06d04";

export let options = {
    stages: [
        { duration: '30s', target: 60 },  // ramp up to 70 users
        { duration: '1m', target: 60 },   // stay at 70 users for 1 minute
        { duration: '30s', target: 0 },   // ramp down to 0 users
    ],
    thresholds: {
        'http_req_duration': ['p(95)<200'],  // 95% of requests must complete below 500ms
    },
    cloud: {
        projectID: 3711451,  // Replace with your project ID
        name: "My API Load Test",  // Replace with a name for your test
    }
};

const BASE_URL = 'http://localhost:8080/api/reviews';

let createReviewDuration = new Trend('create_review_duration');
let getReviewDuration = new Trend('get_review_duration');

export default function () {
    let orderId = generateUUID();
    let userId = generateUUID();

    // Create Review
    let createRes = http.post(`${BASE_URL}`, JSON.stringify({
        orderId: orderId,
        userId: userId,
        restaurantId: RESTAURANT_ID,
        stars: Math.floor(Math.random() * 5) + 1,
        comment: "This is a review comment"
    }), { headers: { 'Content-Type': 'application/json' } });
    createReviewDuration.add(createRes.timings.duration);
    check(createRes, { 'created review successfully': (r) => r.status === 201 });

    // Get Review by Order ID
    let getRes = http.get(`${BASE_URL}/order/${orderId}`);
    getReviewDuration.add(getRes.timings.duration);
    check(getRes, { 'retrieved review successfully': (r) => r.status === 200 });

    sleep(1);
}

export function handleSummary(data) {
    const summaryFileName = `summary-${RESTAURANT_ID}.html`;
    return {
        [summaryFileName]: htmlReport(data),
    };
}
